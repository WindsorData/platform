require 'open-uri'

class SearchController < ApplicationController
  before_filter :find_groups, only: [:quick_search, :full_search]

  def quick_search
    authorize!(:perform, :quick_search)
  end  
  def full_search
    authorize!(:perform, :full_search)
    @primary_roles = PrimaryRole.order("name asc")
    @eq_comp_values = EquityCompensation.order("value asc")
    @cash_comp_values = CashCompensation.order("value asc")
  end

  def results
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8, :role_form)
    json_query = QueryGenerator.json_query(params_hash)
    path = Rails.application.config.backend_host + Rails.application.config.post_query_path

    RestClient.post(path, json_query, {content_type: :json}) do |response, request|
      # do something with the response
    end
    @companies = []
    # RestClient.post(path, query: json_query) do |response, request|
    #   # do something with the response
    #   JSON.parse(response.body)
    # end
    @companies << {ticker: 'appl', full_name: 'Apple Inc'}
    @companies << {ticker: 'goog', full_name: 'Google Inc'} 
  end

  def download
    tickers = params[:tickers]
    # Get file url from backend sending tickers
    file_remote_url = "http://www.sitasingstheblues.com/SitaCueSheet.xls"
    file_name = "#{Time.now.strftime("%Y-%m-%d-%H:%M:%S")}_#{current_user.email}.xls"
    File.open("#{Rails.root.to_s}/tmp/#{file_name}", "wb") do |saved_file|
      open(file_remote_url, 'rb') do |read_file| # open-uri method
        saved_file.write(read_file.read)        
        send_file saved_file.path
      end
    end
  end

  private
  def find_groups
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
  end
end