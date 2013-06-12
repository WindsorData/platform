class FilesController < ApplicationController
  before_filter :authenticate_user!
  before_filter {|c| c.authorize!(:upload, :file)}

  def upload
  end

  def send_file
    case params[:type]
    when 'top5'
      binding.pry
      path = '/api/companies/top5'
    when 'guidelines'
      path = '/api/companies/guidelines'
    when 'dilution'
      path = '/api/companies/dilution'
    when 'batch'
      path = '/api/companies/batch'
    end
    url = Rails.application.config.backend_host + path
    RestClient.post url, dataset: File.new(params[:file].path, 'r')
    redirect_to :back
  end
end