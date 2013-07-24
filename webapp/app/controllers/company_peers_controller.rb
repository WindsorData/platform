class CompanyPeersController < ApplicationController
  def index
    # Get company peers json
    # @companies
  end

  # GET /groups/tickers.json
  def company_peers
    # [{"name":"Demand Media Inc","ticker":"DMD","year":"2011"}]

    @company_peers = CompanyPeer.containing_chars(params[:q])
    
    respond_to do |format|
      format.html
      format.json { render json: @tickers.map(&:attributes) }
    end
  end
end
