# bundle exec rails g scaffold_controller users --no-test-framework
class UsersController < ApplicationController
  inherit_resources

  def create
    params[:user][:company] = Company.find(params[:user][:company]) if params[:user][:role] == "client"
    create! { dashboard_index_path }
  end
end
