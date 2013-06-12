require 'json' # poner en superclase

class SearchController < ApplicationController
  before_filter :authenticate_user!
  before_filter :find_groups, only: [:quick_search, :full_search]

  def quick_search
    authorize!(:perform, :quick_search)
  end  
  def full_search
    # @roles = JSON.parse(RestClient.get('http://192.168.161.176:9000/api/schema/values/roles'))
    # @cash_comp_values = JSON.parse(RestClient.get('http://192.168.161.176:9000/schema/values/cashCompensations'))
  end

  def results
    # get from backend
    mapping_values = Hash[ "role" => "executives.functionalMatches.primary.value" ]
    
    # send to backend
    json_query = Mapping.json_query(params.except(:controller, :action, :authenticity_token, :utf8), mapping_values)
  end

  private
  def find_groups
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
  end
end