class CompaniesController < ApplicationController
  inherit_resources
  authorize_resource

  def index
    @company = Company.new
    @companies = Company.order("name asc").paginated(params)
  end

  def create
    create! { companies_path }
  end

end