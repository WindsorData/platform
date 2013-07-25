class CompanyPeersController < ApplicationController
  def index
    # Get company peers json
    # @companies
  end

  def search_result
    ticker = params[:company_peer_ticker]
    @company_peer = CompanyPeer.find_by_ticker(ticker)

    path = Rails.application.config.backend_host + Rails.application.config.post_incoming_peers_path
    json_query = { ticker: ticker }.to_json

    @companies_peers = []
    RestClient.post(path, json_query, {content_type: :json}) do |response, request|
      if response.code == 200
        @companies_peers = JSON.parse(response.body)
      else
      end
    end
  end

  # GET /company_peers.json
  def company_peers
    @company_peers = CompanyPeer.containing_chars(params[:q])
    # [{"name":"Corba Cumbia Inc","ticker":"CCI"}]
    respond_to do |format|
      format.html
      format.json { render json: @company_peers.map(&:attributes) }
    end
  end
end
