class CompanyPeersController < ApplicationController
  def index
    # Get company peers json
    # @companies
  end

  # GET /groups/tickers.json
  def company_peers
    @company_peers = CompanyPeer.containing_chars(params[:q])
    # [{"name":"Corba Cumbia Inc","ticker":"CCI"}]
    respond_to do |format|
      format.html
      format.json { render json: @company_peers.map(&:attributes) }
    end
  end
end
