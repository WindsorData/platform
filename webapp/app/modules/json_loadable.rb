module JSONLoadable
  def load_json(json)
    create(JSON.parse(json))
  end
end