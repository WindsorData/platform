class CompanyPeersController < ApplicationController
  def incoming_peers
  end

  def peers_peers    
  end

  def incoming_peers_result
    ticker = params[:company_peer_ticker]
    @company_peer = CompanyPeer.find_by_ticker(ticker)

    path = Rails.application.config.backend_host + Rails.application.config.post_incoming_peers_path
    json_query = { ticker: ticker }.to_json

    find_peers(path, json_query)
  end

  def peers_peers_single_ticker_result
    ticker = params[:company_peer_ticker]
    @company_peer = CompanyPeer.find_by_ticker(ticker)
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_single_ticker_path
    json_query = { ticker: ticker }.to_json

    find_peers(path, json_query)
    
  end

  def peers_peers_ticker_list_result
    tickers = params[:company_peer_tickers_manual].split(",")
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_ticker_list_path    
    json_query = { tickers: tickers }.to_json

    find_peers(path, json_query)
    # @companies_peers
  end

  # GET /company_peers.json
  def company_peers
    @company_peers = CompanyPeer.containing_chars(params[:q])
    respond_to do |format|
      format.html
      format.json { render json: @company_peers.map(&:attributes) }
    end
  end
  
  private
  def find_peers(path, json_query)
    @companies_peers = []
    RestClient.post(path, json_query, {content_type: :json}) do |response, request|
      if response.code == 200
        @companies_peers = JSON.parse(response.body)
      else
      end
    end
  end
end
