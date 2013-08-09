class UsersController < ApplicationController
  inherit_resources
  authorize_resource
  before_filter :ensamble_company, only:[:update, :create]

  def edit_account
    @user = current_user
  end

  def update_account
    @user = User.find(current_user.id)
    @user.update_attributes(params[:user])
    sign_in @user, :bypass => true
    redirect_to root_path
  end

  def update
    update! { users_path }
  end

  def create
    create! { users_path }
  end

  def index
    @user = User.new
    @users = User.order("email asc").paginated(params)
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
