# config valid only for Capistrano 3.1
lock '3.1.0'

set :application, 'windsor'
set :repo_url, 'git@github.com:zauberlabs/windsordata-windsordata.git'

set :stages, ["staging", "production"]
set :default_stage, "staging"

set :deploy_to, "/home/ubuntu/windsor"
set :deploy_via, :remote_cache
set :use_sudo, false

# Set up a strategy to deploy only a project directory (not the whole repo)
set :git_strategy, RemoteCacheWithProjectRootStrategy
set :project_root, 'webapp'

set :scm, "git"

set :ssh_options, {
  forward_agent: true,
  keys: "#{Dir.home}/.ssh/windsor.pem"
}

set :rbenv_custom_path, "/opt/rbenv"
set :rbenv_ruby, File.read('.ruby-version').strip

set :pty, true
# set :default_env, { 
#   path: "/opt/rbenv/shims/ruby:$PATH"
# }

set :default_env, {
  'PATH' => "/opt/rbenv/shims/ruby:$PATH"
  # 'RAILS_RELATIVE_URL_ROOT' => "/webapp"
}

set :unicorn_config_path, "config/unicorn.rb"

# set :bundle_gemfile, -> { release_path.join('/webapp/Gemfile') }

# Default branch is :master
# ask :branch, proc { `git rev-parse --abbrev-ref HEAD`.chomp }

# Default deploy_to directory is /var/www/my_app
# set :deploy_to, '/var/www/my_app'

# Default value for :scm is :git
# set :scm, :git

# Default value for :format is :pretty
# set :format, :pretty

# Default value for :log_level is :debug
# set :log_level, :debug

# Default value for :pty is false

# Default value for :linked_files is []
# set :linked_files, %w{config/database.yml}

# Default value for linked_dirs is []
# set :linked_dirs, %w{bin log tmp/pids tmp/cache tmp/sockets vendor/bundle public/system}

# Default value for default_env is {}

# Default value for keep_releases is 5
set :keep_releases, 2

namespace :deploy do


  desc "Upload mailer credentials"
  task :upload_mailer_config do
    on roles(:all), in: :sequence, wait: 5 do
      upload! "config/mailer.yml", "#{release_path}/config/mailer.yml"
    end
  end

  task :restart do
    invoke 'unicorn:restart'
  end

  after :updating, :upload_mailer_config
  after :publishing, :restart

  # desc 'Restart application'
  # task :restart do
  #   on roles(:app), in: :sequence, wait: 5 do
  #     # Your restart mechanism here, for example:
  #     execute :touch, release_path.join('tmp/restart.txt')
  #   end
  # end

  # after :restart, :clear_cache do
  #   on roles(:web), in: :groups, limit: 3, wait: 10 do
  #     # Here we can do anything such as:
  #     # within release_path do
  #     #   execute :rake, 'cache:clear'
  #     # end
  #   end
  # end

end
