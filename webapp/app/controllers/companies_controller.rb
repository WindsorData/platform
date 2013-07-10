class CompaniesController < ApplicationController
  inherit_resources
  authorize_resource

  def index
    @company = Company.new
    @companies = Company.all
    # index!
  end

  def create
    create! { companies_path }
  end

end