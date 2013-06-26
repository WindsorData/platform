class FilesController < ApplicationController
  before_filter :authenticate_user!
  before_filter {|c| c.authorize!(:upload, :file)}

  def single_file
  end

  def send_single
    RestClient.post 'http://192.168.161.176:9000/companies/reports', dataset: File.new(params[:single_file].path, 'r')
    redirect_to :back
  end
end