# bundle exec rails g scaffold_controller users --no-test-framework
class UsersController < ApplicationController
  inherit_resources

  def create
    ensamble_company
    create! { dashboard_index_path }
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
