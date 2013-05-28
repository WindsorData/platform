class SearchController < ApplicationController
  before_filter :authenticate_user!  
  def results
    query = params.except(:controller, :action, :authenticity_token, :utf8).to_json.gsub!(/\"/, '\'').gsub(/'(\d)\'/,' \1')
    binding.pry
    # pegarle al server
  end
end