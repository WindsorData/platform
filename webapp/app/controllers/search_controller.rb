class SearchController < ApplicationController
  before_filter :get_form_values, only: [:full_search, :recent_search]

  def quick_search
    authorize!(:perform, :quick_search)
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
    @users = User.order('email desc')
  end

  def filter_recent_search
    authorize!(:perform, :quick_search)
    user_id =  params["user"].to_i unless params["user"].blank?
    n = params["results"].blank? ? 20 : params["results"].to_i
    if current_user.is_super?
      if user_id
        @searches = Search.last_ordered_by_date(n).where(user_id: user_id)
      else
        @searches = Search.last_ordered_by_date(n)
      end
    elsif current_user.is_client?
      @searches = Search.by_company(current_user.company, n)
    end
  end
  
  def full_search
    authorize!(:perform, :full_search)
  end

  def results
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8, :role_form)
    Top5Search.create(user: current_user, json_query: params_hash.to_json, company: current_user.company)
    json_query = QueryGenerator.json_query(params_hash)
    path = Rails.application.config.backend_host + Rails.application.config.post_query_path
    headers ={content_type: :json}

    @tickers = []
    RestClient::Request.execute(:method => :post, :url => path, :payload => json_query, :headers => headers, :timeout => -1)  do |response, _|
      @tickers = JSON.parse(response)
    end
  end

  def download
    json_query = { range: params[:range].to_i, companies: params[:companies]}.to_json
    perform_search(json_query, Constants::TOP5_REPORT)
  end

  def group_search
    group = Group.find(params[:group])
    tickers = group.tickers.map(&:ticker)
    CompanyPeerSearch.create(user: current_user, company: current_user.company, tickers: tickers.join(";"), group_name: group.name, report_type: params[:report])
    json_query = { range: 3, companies: group.tickers.map(&:cusip) }.to_json
    perform_search(json_query, params[:report])
  end

  def recent_search
    authorize!(:perform, :full_search)    
    @params_hash = JSON.parse(Top5Search.find(params[:id]).json_query)

    render "top_5_recent_search_log"
  end

  def search_log
    @search = Search.find(params[:id])
  end

  private
  def get_form_values
    @primary_roles = PrimaryRole.order("name asc")
    @eq_comp_values = EquityCompensation.order("value asc")
    @cash_comp_values = CashCompensation.order("value asc")    
  end

  def perform_search(json_query, report_type)
    case report_type
      when Constants::TOP5_REPORT
        path = Rails.application.config.backend_host + Rails.application.config.post_download_top5_report_path
      when Constants::BOD_REPORT
        path = Rails.application.config.backend_host + Rails.application.config.post_download_bod_report_path
      when Constants::FULL_REPORT
        path = Rails.application.config.backend_host + Rails.application.config.post_download_full_report_path
    end

    report_request(path, json_query, "report.xls")
  end
end
