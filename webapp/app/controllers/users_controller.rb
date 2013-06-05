# bundle exec rails g scaffold_controller users --no-test-framework
class UsersController < ApplicationController
  inherit_resources
  load_and_authorize_resource

  def create
    ensamble_company
    create!
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
