@Library('shared-library@v2022.03.05-1') _

def pipelineConfig = [
    "stackName": "protocol-solana",
    "services": [
        [name: 'solana-migration', path: './solana-migration'],
        [name: 'solana-indexer', path: './solana-indexer'],
        [name: 'solana-api', path: './solana-api']
    ],
    "slackChannel": "#protocol-duty"
]

serviceCI(pipelineConfig)
