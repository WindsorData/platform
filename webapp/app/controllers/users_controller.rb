# bundle exec rails g scaffold_controller users --no-test-framework
class UsersController < ApplicationController
  inherit_resources
  authorize_resource
  before_filter :ensamble_company, only:[:update, :create]

  def update
    update! { users_path }
  end

  def create
    create! { users_path }
  end

  private
  def ensamble_company
    if params[:user][:role] == "client"
      params[:user][:company] = Company.find(params[:user][:company])
    else
      params[:user][:company] = nil
    end 
  end

end
