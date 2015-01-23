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

  def companies_inventory_file
    database_inventory_file('companies')
  end

  def peers_inventory_file
    database_inventory_file('peers')
  end


  def delete_top5
    delete_company('top5')
  end

  def delete_bod
    delete_company('bod')
  end

  def delete_db
    path = Rails.application.config.backend_host + Rails.application.config.drop_all_peers_path
    authorize!(:perfom, :delete_info)
    backend_delete(path)
  end

  def delete_peers
    company_peer = params[:company_peer_ticker]
    peer = params[:peer_ticker]
    path = Rails.application.config.backend_host + "/api/companies/peers"
    authorize!(:perfom, :delete_info)

    payload = {
      fiscalYear: params[:fiscal_year].to_i,
      fillingDate: params[:filling_date],
      from: company_peer,
      peer: peer
    }

    RestClient::Request.execute(
      :method => :delete, 
      :url => path, 
      :payload => payload.to_json, 
      :headers => {content_type: :json}) do |response, _|
      if response.code == 200
        flash[:notice] = "Information deleted successfully for company peer #{company_peer}, peer #{peer}" 
      else
        flash[:errors] = JSON.parse(response)["error"]
      end
    end

    render 'delete_info'
  end

  def backend_delete(path)
    RestClient.delete(path, {accept: :json}) do |response, _|
      if response.code == 200
        if block_given?
          yield(response)
        else
          flash[:notice] = "Information deleted successfully"
        end
      else
        flash[:errors] = JSON.parse(response)["error"]
      end
    end
    render 'delete_info'
  end

  private

  def delete_company(type)
    authorize!(:perfom, :delete_info)
    path = Rails.application.config.backend_host + "/api/companies/#{type}/#{params[:ticker]}/year/#{params[:year]}"
    backend_delete(path)
  end

  def database_inventory_file(type)
    @data = JSON.parse(RestClient.get("http://localhost:9000/api/companies/inventory/#{type}"))
    respond_to do |format|
      format.xls { render 'database_inventory'}
    end
  end
end