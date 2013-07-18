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

    @tickers = []
    RestClient.post(path, json_query, {content_type: :json}) do |response, request|
      @tickers = JSON.parse(response)
    end
  end

  def download
    json_query = params.except(:controller, :action, :authenticity_token, :utf8, :role_form).to_json.gsub(/(")(\d+)(")/, ' \2')
    path = Rails.application.config.backend_host + Rails.application.config.post_download_report_path  
    RestClient.post(path, json_query, {content_type: :json}) do |response, request|
      if response.code == 200
        send_data(response.body, filename: "elexcel.xls")
      else
        flash[:error] = "There was an error"
        render "results"
      end
    end
  end

  def group_search
    tickers = Group.find(params[:group]).tickers.map(&:ticker)
    perform_search(tickers, params[:report])
  end

  private
  def find_groups
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
  end

  # TODO: Use in full and groups search
  def perform_search(tickers, report_type)
    path = Rails.application.config.backend_host
  end

end