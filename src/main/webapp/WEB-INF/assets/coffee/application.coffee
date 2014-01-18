$(->
  $("a.rename").click (e) ->
    unless confirm("Rename with metadata?")
      e.preventDefault()
)

