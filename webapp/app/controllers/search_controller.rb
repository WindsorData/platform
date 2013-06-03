class SearchController < ApplicationController
  before_filter :authenticate_user!

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
end