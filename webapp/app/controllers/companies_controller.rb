class CompaniesController < ApplicationController
  inherit_resources
  authorize_resource

  def index
    @company = Company.new
    @companies = Company.order("name asc").page(params[:page]).per(15)
  end

  def create
    create! { companies_path }
  end

end