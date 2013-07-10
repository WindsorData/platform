class SearchController < ApplicationController
  before_filter :authenticate_user!
  before_filter :find_groups, only: [:quick_search, :full_search]

  def quick_search
    authorize!(:perform, :quick_search)
  end  
  def full_search
    authorize!(:perform, :full_search)
    @primary_roles = PrimaryRole.all
    @eq_comp_values = EquityCompensation.all
    @cash_comp_values = CashCompensation.all
  end

  def results
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8, :role_form)
    json_query = QueryGenerator.json_query(params_hash)
    path = Rails.application.config.backend_host

    RestClient.post(path, query: json_query) do |response, request|
      # do something with the response
    end
  end

  private
  def find_groups
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
  end
end