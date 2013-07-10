class UsersController < ApplicationController
  inherit_resources
  authorize_resource
  before_filter :ensamble_company, only:[:update, :create]

  def update
    update! { users_path }
  end

  # def create
  #   create! { users_path }
  # end

  def index
    @user = User.new
    @users = User.order("email asc").page(params[:page]).per(15)
    index!
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
