(ns knotlog.domain.file)

(defrecord File [id create-time uri-path piece-id])

(defn format-upload-result
  "Format the result of a file upload operation"
  [files]
  {:files (map :filename files)})

(defn format-error-result
  "Format an error result"
  [error-message]
  {:message error-message})
