# bundle exec rails g scaffold_controller groups index new --no-test-framework
class GroupsController < ApplicationController
  before_filter :authenticate_user!
  inherit_resources
  before_filter {|c| c.authorize!(:create, Group)}

  def create
    @group = Group.new(params[:group])
    @group.company = current_user.company if current_user.is_client?
    create! { quick_search_path }
  end
  
  # GET /groups/tickers.json
  def tickers    
    @tickers = Ticker.containing_chars(params[:q])
    respond_to do |format|
      format.html
      format.json { render json: @tickers.map(&:attributes) }
    end
  end
end