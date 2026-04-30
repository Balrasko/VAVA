{{/*
Expand the chart name.
*/}}
{{- define "vava.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "vava.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Common labels.
*/}}
{{- define "vava.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ include "vava.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels.
*/}}
{{- define "vava.selectorLabels" -}}
app.kubernetes.io/name: {{ include "vava.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Secret name used for database credentials.
*/}}
{{- define "vava.postgresql.secretName" -}}
{{- default (printf "%s-postgresql" (include "vava.fullname" .)) .Values.postgresql.existingSecret -}}
{{- end -}}

{{/*
ConfigMap name used for database init scripts.
*/}}
{{- define "vava.postgresql.initConfigMapName" -}}
{{- default (printf "%s-postgresql-initdb" (include "vava.fullname" .)) .Values.postgresql.initdb.existingConfigMap -}}
{{- end -}}
