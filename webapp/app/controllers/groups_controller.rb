# bundle exec rails g scaffold_controller groups index new --no-test-framework
class GroupsController < ApplicationController
 
  # GET /groups/new
  # GET /groups/new.json
  def new
    @group = Group.new

    respond_to do |format|
      format.html # new.html.erb
    end
  end

  # POST /groups
  # POST /groups.json
  def create
    @group = Group.new(params[:group])
    respond_to do |format|
      if @group.save
        format.html { redirect_to dashboard_index_path, notice: 'Group was successfully created.' }
      else
        format.html { render action: "new" }
      end
    end
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