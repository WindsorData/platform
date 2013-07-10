class ApplicationController < ActionController::Base
  protect_from_forgery
  before_filter :authenticate_user!

  def after_sign_in_path_for(resource)
    user_root_path(resource)
  end

  def after_sign_out_path_for(resource_or_scope)
    new_user_session_path
  end

  helper_method :user_root_path
  def user_root_path(user)
    case user.role
    when 'super'
      quick_search_path
    when 'admin'
      file_upload_path
    when 'client'
      quick_search_path
    end
  end

  rescue_from CanCan::AccessDenied do
    render "#{Rails.root}/public/401"
  end

  rescue_from RestClient::InternalServerError do
    render "#{Rails.root}/public/500"
  end
  
  rescue_from Errno::EHOSTUNREACH do
    flash[:error] = "Unable to connect to backend host"
    redirect_to :back
  end

  rescue_from Errno::ECONNREFUSED do
    flash[:error] = "Backend connection refused"
    redirect_to :back
  end

end
