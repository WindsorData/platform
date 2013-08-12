#
# Cookbook Name:: application
# Recipe:: default
#
# Copyright 2013, Windsor
#
# All rights reserved - Do Not Redistribute
#
############################### Versions  ######################################
java_version = "7"
play_version = "2.1.0"

########################## Well known directories ##############################
rails_app_path = "/vagrant/webapp"
play_app_path = "/vagrant/core"

########################## Install General Commands ############################
include_recipe "set_locale"

package "tree"
package "vim"
package "libxml2-dev"
package "libxslt1-dev"
package "wget"
package "unzip"


################################ Install Play  #################################
package "openjdk-#{java_version}-jdk"

execute "Install Play" do
  play_archive = "play-#{play_version}"
  command """wget http://downloads.typesafe.com/play/#{play_version}/#{play_archive}.zip && \
             unzip #{play_archive}.zip && \
             mv #{play_archive} play"""
end


################################ Install Mongo  ################################
include_recipe "mongodb::default"

######################### Install Backend Server ###############################

execute "Compile Backend" do
  command "cd #{play_app_path} && /play/play clean compile stage"
end



###################### Install PostgresSQL databases ###########################
include_recipe "postgresql_server_utf8"
include_recipe "database::postgresql"

postgresql_connection = {
  :host => 'localhost', 
  :port => 5432, 
  :username => 'postgres', 
  :password => 'postgres'
}

postgresql_database_user 'windsor' do
  connection postgresql_connection
  password 'windsor'
  database_name 'windsor_development'
  privileges ["ALL"]
  action :create
end

postgresql_database 'windsor_development' do
  connection postgresql_connection
  encoding 'UTF-8'
  owner 'windsor'
  action :create
end

postgresql_database 'windsor_test' do
  connection postgresql_connection
  encoding 'UTF-8'
  owner 'windsor'
  action :create
end

bash "Grant createdb to windsor user" do
  user "postgres"
  code 'psql -c "ALTER USER windsor CREATEDB"'
end

################################# Install Ruby #################################
include_recipe "rbenv::default"
include_recipe "rbenv::ruby_build"

rbenv_ruby "1.9.3-p327" do
  global true
end

rbenv_gem "bundler" do
  ruby_version "1.9.3-p327"
end

############################### Install Rails Server ###########################
execute "bundle install" do
  command "cd #{rails_app_path} && rbenv exec bundle install --without staging development test integration"
end

execute "rake db:migrate" do
  command "cd #{rails_app_path} && rbenv exec bundle exec rake db:migrate"
end

execute "rake db:seed" do
  command "cd #{rails_app_path} && rbenv exec bundle exec rake db:seed"
end

############################ Start Play Server  ################################
execute "Start Play Server" do
  command "cd #{play_app_path} && nohup target/start &"
end

############################ Start Rails Server ################################
execute "Start Rails Server" do
  command "cd #{rails_app_path} && rbenv exec bundle exec rails server -d"
end

#################### Add Rails & Play Servers Autostart ########################

cookbook_file "/etc/rc.local" do
  source "rc.local"
  mode 0755
  owner "root"
  group "root"
end