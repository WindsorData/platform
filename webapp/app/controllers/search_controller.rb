class SearchController < ApplicationController
  before_filter :authenticate_user!  
  def results
    binding.pry
  end
end
