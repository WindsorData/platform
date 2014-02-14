class FilesController < ApplicationController
  before_filter {|c| c.authorize!(:upload, :file)}

  ROUTES_UPLOAD = {     Constants::TOP_5_FILE => Rails.application.config.post_top5_path,
                        Constants::GUIDELINESS_FILE => Rails.application.config.post_guidelines_path,
                        Constants::DILUTION_FILE => Rails.application.config.post_dilution_path,
                        Constants::PEERS_FILE => Rails.application.config.peers_path,
                        Constants::BOD_FILE => Rails.application.config.post_bod_path,
                        Constants::BATCH_FILE_COMPANIES => Rails.application.config.post_batch_companies_path,
                        Constants::BATCH_FILE_BOD => Rails.application.config.post_batch_bod_path,
                        Constants::BATCH_FILE_PEERS => Rails.application.config.post_batch_peers_path
                   }

  def send_file
    result = post_file(params[:type], params[:file])
    @upload_log = save_result_upload(params[:type], result)
    BackendService.update_search_values
    render 'files/upload'
  end

  private
  def post_file(type, file)
    url = Rails.application.config.backend_host + path_by_upload_type(type)
    payload = {
        filename: params[:file].original_filename,
        dataset: File.new(file.path, 'r')
    }
    headers = {accept: :json}
 
    RestClient::Request.execute(:method => :post, :url => url, :payload => payload, :headers => headers, :timeout => -1)  do |response, _|
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
              ticker: result["ticker"],
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
    ROUTES_UPLOAD[type]
  end

end