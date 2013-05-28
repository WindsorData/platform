class Mapping

  # mapping_values = Hash[ "rol_primary" => "executives.functionalMatches.primary.value" ]
  # params = { "rol_primary" => "CEO", "Salary" => 20 }
  def self.json_query(params, mapping_values)
    Hash[params.map {|k, v| [mapping_values[k] || k, v] }].to_json.gsub!(/\"/, '\'').gsub(/'(\d)\'/,' \1')
  end
end