class ApplicationController < ActionController::Base
  protect_from_forgery
  helper_method :user_root_path # Available for views

  def after_sign_in_path_for(resource)
    user_root_path(resource)
  end

  def after_sign_out_path_for(resource_or_scope)
    new_user_session_path
  end

  rescue_from CanCan::AccessDenied do |exception|
    render "#{Rails.root}/public/401"
  end

  def user_root_path(user)
    case user.role
    when 'super'
      users_path
    when 'admin'
      single_file_upload_path
    when 'client'
      quick_search_path
    end
  end

end
