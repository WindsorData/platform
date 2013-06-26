class SearchController < ApplicationController
  before_filter :authenticate_user!
  before_filter :find_groups, only: [:quick_search, :full_search]

  def quick_search
    authorize!(:perform, :quick_search)
  end  
  def full_search
    @roles = Role.all
    @eq_comp_values = EquityCompensation.all
    @cash_comp_values = CashCompensation.all
  end

  def results
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8, :role_form)
    json_query = QueryGenerator.json_query(params_hash)
    BackendService.perform_search(json_query)
  end

  private
  def find_groups
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
  end
end