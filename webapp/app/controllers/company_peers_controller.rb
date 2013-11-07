class CompanyPeersController < ApplicationController
  def incoming_peers
  end

  def peers_peers    
  end

  def incoming_peers_result
    ticker = params[:company_peer_ticker]
    @company_peer = CompanyPeer.find_by_ticker(ticker)

    path = Rails.application.config.backend_host + Rails.application.config.post_incoming_peers_path
    @json_query = { ticker: ticker }.to_json

    find_peers(path, @json_query)
    peers = @companies_peers.map{ |x| "#{x["companyName"]}(#{x["ticker"]})"}.join(";")
    IncomingPeersSearch.create(user: current_user, company: current_user.company, tickers: ticker, peers: peers)
  end

  def peers_peers_single_ticker_result
    ticker = params[:company_peer_ticker]
    @company_peer = CompanyPeer.find_by_ticker(ticker)
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_single_ticker_path
    @json_query = { ticker: ticker }.to_json

    find_peers(path, @json_query)
    peers = @companies_peers["primaryPeers"].map{ |x| "#{x["peerCoName"]}(#{x["peerTicker"]})"}.join(";")
    PeersPeersSearch.create(user: current_user, company: current_user.company, tickers: ticker, peers: peers)
    render "peers_peers_result"
  end

  def peers_peers_ticker_list_result
    search_tickers = params[:company_peer_tickers_manual].split(" ").map(&:upcase)
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_ticker_list_path    
    @json_query = { tickers: search_tickers }.to_json
    find_peers(path, @json_query)
    
    peers = @companies_peers["primaryPeers"].map{ |x| "#{x["peerCoName"]}(#{x["peerTicker"]})"}.join(";")
    PeersPeersSearch.create(user: current_user, company: current_user.company, peers: peers)
    render "peers_peers_result" 
  end

  # GET /company_peers.json
  def company_peers
    @company_peers = CompanyPeer.containing_chars(params[:q])
    respond_to do |format|
      format.html
      format.json { render json: @company_peers.map(&:attributes) }
    end
  end

  def incoming_peers_raw_data_file
    path = Rails.application.config.backend_host + Rails.application.config.post_incoming_peers_raw_data_path
    report_request(path, params[:json_query], "raw_data.xls")
  end

  def peers_peers_single_ticker_file
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_single_ticker_path
    download_peers_peers_file(path)
  end

  def peers_peers_raw_data_file
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_raw_data_path
    report_request(path, params[:json_query], "raw_data.xls")
  end

  def peers_peers_raw_data_file_from_primary
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_ticker_list_raw_data_path
    report_request(path, params[:json_query], "raw_data.xls")
  end

  def peers_peers_ticker_list_file
    path = Rails.application.config.backend_host + Rails.application.config.post_peers_peers_ticker_list_path
    download_peers_peers_file(path)
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

  def download_peers_peers_file(path)
    json_query = params[:json_query]
    
    find_peers(path, json_query)
    @primary_peers = @companies_peers["primaryPeers"]
    @companies_peers = (@companies_peers["normalized"] + @companies_peers["unnormalized"]).group_by { |p| p["secondPeer"] }

    
    respond_to do |format|
      format.xls { render 'peers_peers_result'}
    end
  end
end
