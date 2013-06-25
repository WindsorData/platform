class FilesController < ApplicationController
  before_filter :authenticate_user!
  before_filter {|c| c.authorize!(:upload, :file)}

  def upload
  end

  def send_file
    #BackendService.post_file(params[:type], params[:file])
    BackendService.update_search_values
    redirect_to :back
  end

  rescue_from Errno::EHOSTUNREACH, with: :render_500
  rescue_from Errno::ECONNREFUSED, with: :render_500
  rescue_from RestClient::InternalServerError, with: :render_500

end