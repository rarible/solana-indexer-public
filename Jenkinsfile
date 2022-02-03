@Library('shared-library@v20211117-1') _

def pipelineConfig = [
    "stackName": "protocol-solana",
    "services": [
        [name: 'solana-indexer', path: './solana-indexer'],
        [name: 'solana-api', path: './solana-api']
    ],
    "slackChannel": "#protocol-duty"
]

serviceCI(pipelineConfig)
