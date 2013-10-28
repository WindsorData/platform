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
    @user = User.new(params[:user])
    password = @user.generate_random_password
    if @user.save
      flash[:notice] = "Email: " + @user.email + " Password: " + password
    end
    redirect_to users_path
  end

  def index
    @user = User.new
    @users = User.order("email asc").paginated(params)
    index!
  end

  def destroy
    Search.where(user_id: params["id"]).each do |s|
      s.destroy
    end
    destroy!
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