# bundle exec rails g scaffold_controller groups index new --no-test-framework
class GroupsController < ApplicationController
  inherit_resources
  before_filter {|c| c.authorize!(:create, Group)}

  def create
    @group = Group.new(params[:group])
    @group.company = current_user.company #current_user is always a client
    create! { quick_search_path }
  end
  
  # GET /groups/tickers.json
  def tickers    
    @tickers = Ticker.where("name like ?", "%#{params[:q]}%")
    respond_to do |format|
      format.html
      format.json { render json: @tickers.map(&:attributes) }
    end
  end
end