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
    @tickers = []
    @tickers << {ticker: 'appl', name: 'Apple Inc'}
    @tickers << {ticker: 'goog', name: 'Google Inc'} 
  end

  def download
    tickers = params[:tickers]
    perform_search(tickers, Constants::TOP5_REPORT)
  end

  def group_search
    tickers = Group.find(params[:group]).tickers.map(&:ticker)
    perform_search(tickers, params[:report])
  end

  private
  def find_groups
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
  end

  def perform_search(tickers, report_type)
    path = Rails.application.config.backend_host
    # RestClient.post(path, tickers: tickers) do |response, request|
    #   JSON.parse(response.body)
    #   file_name = "#{Time.now.strftime("%Y-%m-%d-%H:%M:%S")}_#{current_user.email}.xls"
    #   File.open("#{Rails.root.to_s}/tmp/#{file_name}", "wb") do |file|        
    #     file.write(response.body)        
    #     send_file file.path
    #   end
    # end
  end

end