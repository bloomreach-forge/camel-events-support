# Apache Camel - Hippo Events Support

Apache Camel - Hippo Events Support provides: 
- A **hippoevent:** Apache Camel component
- A Camel Repository Scheduler Job component which can invoke a Camel Endpoint URI
- Utility classes to help integration between Hippo CMS/Repository and Apache Camel.

# Documentation

Documentation is available at [bloomreach-forge.github.io/camel-events-support](https://bloomreach-forge.github.io/camel-events-support)

Docs are built automatically from the release tag and deployed to the `gh-pages` branch by the [Deploy Docs](.github/workflows/deploy-docs.yml) workflow. They can also be triggered manually via `workflow_dispatch`.

To preview docs locally:

```
mvn clean site:site
```

The output will be in `target/site/` and is ignored by Git.

# Release Process

This project uses [git-flow](https://bloomreach-forge.github.io/using-git-flow.html) for releases with automated deployment.

## Steps

1. **Start release and set version**
   ```bash
   git flow release start x.y.z
   mvn versions:set -DgenerateBackupPoms=false -DnewVersion="x.y.z"
   mvn -f demo/pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion="x.y.z"
   git commit -a -m "<ISSUE_ID> releasing x.y.z: set version"
   ```

2. **Finish release** (creates tag, merges to master/develop)
   ```bash
   git flow release finish x.y.z
   ```

3. **Set next snapshot and push** (you're now on develop)
   ```bash
   mvn versions:set -DgenerateBackupPoms=false -DnewVersion="x.y.z+1-SNAPSHOT"
   mvn -f demo/pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion="x.y.z+1-SNAPSHOT"
   git commit -a -m "<ISSUE_ID> releasing x.y.z: set next development version"
   git push origin develop master --follow-tags
   ```

> Replace `<ISSUE_ID>` with your JIRA ticket (e.g., `FORGE-123`).

The [Release](.github/workflows/release.yml) workflow triggers automatically on the semver tag and will:

1. Verify the tag matches the version in `pom.xml` and `demo/pom.xml`
2. Build and test the project and demo
3. Deploy the artifact to the Bloomreach Forge Maven repository
4. Create a GitHub Release with auto-generated notes

Once the release workflow completes successfully, the [Deploy Docs](.github/workflows/deploy-docs.yml) workflow runs automatically and publishes the updated site to `gh-pages`.

### Branch model

| Branch | Purpose |
|---|---|
| `develop` | Active development |
| `master` | Prerelease staging — release tags are cut from here |
| `gh-pages` | Published documentation (managed by CI, do not edit manually) |