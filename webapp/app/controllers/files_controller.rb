class FilesController < ApplicationController
  before_filter :authenticate_user!
  before_filter {|c| c.authorize!(:upload, :file)}

  def upload
  end

  def send_file
    post_file(params[:type], params[:file])
    BackendService.update_search_values
    redirect_to :back
  end

  private
  def post_file(type, file)
    path = Rails.application.config.backend_host
    case type
    when 'top5'
      path += Rails.application.config.post_top5_path
    when 'guidelines'
      path += Rails.application.config.post_guidelines_path
    when 'dilution'
      path += Rails.application.config.post_dilution_path
    when 'batch'
      path += Rails.application.config.post_batch_path
    end
    RestClient.post(path, {dataset: File.new(file.path, 'r')}, {accept: :json}) do |response, request|
      if response.code == 200
        flash[:notice] = "Upload successfully completed"
      else
        flash[:error] = "The uploaded file is invallid"
      end
    end
  end
end