class FilesController < ApplicationController
  before_filter {|c| c.authorize!(:upload, :file)}

  def send_file
    result = post_file(params[:type], params[:file])
    BackendService.update_search_values
    @upload_log = save_result_upload(params[:type], result)
    render 'files/upload'
  end

  private
  def post_file(type, file)
    path = Rails.application.config.backend_host + path_by_upload_type(type)
    RestClient.post(path, {dataset: File.new(file.path, 'r')}, {accept: :json}) do |response, _|
      if response.code == 200
        flash[:notice] = "Upload successfully completed"
      else
        flash[:error] = "The uploaded file is invalid"
      end
      response
    end
  end

  private
  def save_result_upload(type, result_upload)
    results = JSON.parse(result_upload)["results"]
    details = results.collect { |result|
      DetailUploadFile.create(
          {
              file_name: result["file"],
              ticker: Ticker.find_by_cusip(result["cusip"]),
              messages: result["messages"].to_s
          }
      )
    }

    UploadLog.create(
        {
            upload_type: type,
            user: current_user,
            detail_upload_files: details
        }
    )
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
      when 'peers'
        Rails.application.config.post_peers_path
      # add peers data case
    end
  end

end