class ApplicationController < ActionController::Base
  protect_from_forgery

  def after_sign_in_path_for(resource)
    if resource.role == 'super'
      users_path
    elsif resource.role == 'admin'
      single_file_upload_path # Change for upload file page
    elsif resource.role == 'client'
      quick_search_path      
    end
  end

  def after_sign_out_path_for(resource_or_scope)
    new_user_session_path
  end

  rescue_from CanCan::AccessDenied do |exception|
    render "#{Rails.root}/public/401"
  end

end
