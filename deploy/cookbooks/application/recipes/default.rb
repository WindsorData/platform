#
# Cookbook Name:: application
# Recipe:: default
#
# Copyright 2013, Windsor
#
# All rights reserved - Do Not Redistribute
#
java_version = "7"
play_version = "2.1.0"

include_recipe "set_locale"
include_recipe "rbenv::default"
include_recipe "rbenv::ruby_build"
include_recipe "mongodb::default"

package "tree"
package "vim"
package "libxml2-dev"
package "libxslt1-dev"
package "wget"
package "unzip"
package "openjdk-#{java_version}-jdk"



################### Setting up Play Backend  ###################################
execute "Install Play" do
  play_archive = "play-#{play_version}"
  command """wget http://downloads.typesafe.com/play/#{play_version}/#{play_archive}.zip && \
             unzip #{play_archive}.zip && \
             mv #{play_archive} play"""
end

execute "Compile Backend" do
  command "cd core && /play/play clean compile stage"
end

################### Setting up PostgresSQL databases ###########################
include_recipe "postgresql_server_utf8"
include_recipe "database::postgresql"

webapp_path = "/vagrant/webapp"

postgresql_connection = {
  :host => 'localhost', 
  :port => 5432, 
  :username => 'postgres', 
  :password => 'postgres'
}

# Creates skillhub user
postgresql_database_user 'windsor' do
  connection postgresql_connection
  password 'windsor'
  database_name 'windsor_dev'
  privileges ["ALL"]
  action :create
end

# Creates development database
postgresql_database 'windsor_dev' do
  connection postgresql_connection
  encoding 'UTF-8'
  owner 'windsor'
  action :create
end

# Creates test database
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
rbenv_ruby "1.9.3-p327" do
  global true
end

rbenv_gem "bundler" do
  ruby_version "1.9.3-p327"
end
################################ Run Server ####################################

execute "bundle install" do
  command "cd #{webapp_path} && rbenv exec bundle install --without staging development test"
end

execute "rake db:migrate" do
  command "cd #{webapp_path} && bundle exec rake db:migrate"
end

execute "rake db:seed" do
  command "cd #{webapp_path} && bundle exec rake db:seed"
end

execute "rails server" do
  command "cd #{webapp_path} && rails server"
end
