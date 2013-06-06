class SearchController < ApplicationController
  before_filter :authenticate_user!
  before_filter :find_groups, only: [:quick_search, :full_search]

  def quick_search
  end
  
  def full_search
  end

  def results
    # get from backend
    mapping_values = Hash[ "role" => "executives.functionalMatches.primary.value" ]
    
    # send to backend
    json_query = Mapping.json_query(params.except(:controller, :action, :authenticity_token, :utf8), mapping_values)
  end

  private
  def find_groups
    @groups = Group.by_company(current_user.company)
  end
end