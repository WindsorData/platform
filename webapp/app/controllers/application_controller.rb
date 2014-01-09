class ApplicationController < ActionController::Base
  extend Pagination

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
    quick_search_path
  end

  def report_request(path, payload, filename)
    RestClient::Request.execute(
      :method => :post, 
      :url => path, 
      :payload => payload, 
      :headers => {content_type: :json}, 
      :timeout => -1) do |response, _|

        if response.code == 200
          send_data(response.body, filename: filename)
        elsif response.code == 404
          render "results"
        else
          flash[:error] = "There was an error"
          render "results"
        end
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
