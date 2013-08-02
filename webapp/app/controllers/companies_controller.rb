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

  def delete_info
    authorize!(:perfom, :delete_info)
  end

  def perform_info_deletion
    authorize!(:perfom, :delete_info)
    path = Rails.application.config.backend_host + "/api/companies/#{params[:ticker]}/year/#{params[:year]}"
    RestClient.delete(path, {accept: :json}) do |response, _|
      if response.code == 200
        flash[:notice] = "Information deleted successfully"
      end
    end
    render 'delete_info'
  end
end