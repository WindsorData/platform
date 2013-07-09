class FilesController < ApplicationController
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
    path = Rails.application.config.backend_host + path_by_upload_type(type)
    RestClient.post(path, {dataset: File.new(file.path, 'r')}, {accept: :json}) do |response, _|
      if response.code == 200
        flash[:notice] = "Upload successfully completed"
      else
        flash[:error] = "The uploaded file is invallid"
      end
    end
  end

  def path_by_upload_type(type)
    case type
      when 'top5'
        Rails.application.config.post_top5_path
      when 'guidelines'
        Rails.application.config.post_guidelines_path
      when 'dilution'
        Rails.application.config.post_dilution_path
      when 'batch'
        Rails.application.config.post_batch_path
    end
  end

end