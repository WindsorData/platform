class FilesController < ApplicationController
  before_filter :authenticate_user!
  before_filter {|c| c.authorize!(:upload, :file)}
  after_filter :authorize, :only => :delete

  def upload
  end

  def send_file
    BackendService.post_file(params[:type], params[:file])
    BackendService.load_tickers
    BackendService.load_roles
    redirect_to :back
  end

  rescue_from Errno::EHOSTUNREACH do
    render "#{Rails.root}/public/500"
  end

  rescue_from RestClient::InternalServerError do
    render "#{Rails.root}/public/500"
  end 
end