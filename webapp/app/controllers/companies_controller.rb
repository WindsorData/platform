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
    backend_delete(path)    
  end

  def delete_db
    path = Rails.application.config.backend_host + Rails.application.config.peers_path
    authorize!(:perfom, :delete_info)    
    backend_delete(path)
  end

  def delete_peers
    company_peer = params[:company_peer_ticker]
    path = Rails.application.config.backend_host + "/api/companies/peers/#{company_peer}"
    authorize!(:perfom, :delete_info)
    
    backend_delete(path) do |response|
      message = 
        "Information deleted successfully for company peer #{company_peer}<br/><br/>" + 
        "Peers tickers removed:<br/><br/>" +
        "<ul>" +
          JSON.parse(response).map { |peer| "<li>#{peer["peerCoName"]} (#{peer["peerTicker"]})</li>"}.join +
        "</ul>"
      flash[:notice] = message.html_safe

      CompanyPeer.where(ticker: company_peer).destroy_all
    end
  end

  def backend_delete(path)
    RestClient.delete(path, {accept: :json}) do |response, _|
      if response.code == 200
        if block_given?
          yield(response)
        else
          flash[:notice] = "Information deleted successfully"
        end
      end
    end
    render 'delete_info'    
  end
end