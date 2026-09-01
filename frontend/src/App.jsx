import { useCallback, useEffect, useState } from "react";

import {
  fetchAuthSession,
  getCurrentUser,
  signIn,
  signOut,
} from "aws-amplify/auth";

import "./amplify";
import "./App.css";

const emptyIncident = {
  title: "",
  description: "",
  severity: "LOW",
  status: "OPEN",
  owner: "",
};

async function getAccessToken() {
  const session = await fetchAuthSession();

  const token =
    session.tokens?.accessToken?.toString();

  if (!token) {
    throw new Error(
      "No authentication token available."
    );
  }

  return token;
}

async function checkAdminAccess() {
  const session = await fetchAuthSession();

  const groups =
    session.tokens?.accessToken?.payload?.[
      "cognito:groups"
    ] ?? [];

  return (
    Array.isArray(groups) &&
    groups.includes("Admins")
  );
}

function App() {
  const [user, setUser] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [incidents, setIncidents] = useState([]);

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] =
    useState(false);

  const [showForm, setShowForm] = useState(false);
  const [editingIncident, setEditingIncident] =
    useState(null);

  const [incidentForm, setIncidentForm] =
    useState(emptyIncident);

  const loadIncidents = useCallback(async () => {
    try {
      setError("");

      const accessToken =
        await getAccessToken();

      const response = await fetch(
        `${import.meta.env.VITE_API_URL}/api/incidents`,
        {
          headers: {
            Authorization:
              `Bearer ${accessToken}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error(
          `API request failed: ${response.status}`
        );
      }

      const data = await response.json();

      setIncidents(data);
    } catch (err) {
      console.error(err);

      setError(
        "Unable to load incidents."
      );
    }
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function initializeApp() {
      try {
        const currentUser =
          await getCurrentUser();

        if (cancelled) {
          return;
        }

        setUser(currentUser);

        const adminAccess =
          await checkAdminAccess();

        if (cancelled) {
          return;
        }

        setIsAdmin(adminAccess);

        await loadIncidents();
      } catch {
        if (!cancelled) {
          setUser(null);
          setIsAdmin(false);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void initializeApp();

    return () => {
      cancelled = true;
    };
  }, [loadIncidents]);

  async function handleLogin(event) {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      const result = await signIn({
        username,
        password,
        options: {
          authFlowType:
            "USER_PASSWORD_AUTH",
        },
      });

      if (!result.isSignedIn) {
        setError(
          "Additional sign-in step required."
        );

        return;
      }

      const currentUser =
        await getCurrentUser();

      setUser(currentUser);

      const adminAccess =
        await checkAdminAccess();

      setIsAdmin(adminAccess);

      setPassword("");

      await loadIncidents();
    } catch (err) {
      console.error(err);

      setUser(null);
      setIsAdmin(false);

      setError(
        err.message ||
          "Unable to sign in."
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleLogout() {
    await signOut();

    setUser(null);
    setIsAdmin(false);
    setIncidents([]);
    setUsername("");
    setPassword("");

    closeForm();
  }

  function handleFormChange(event) {
    const { name, value } =
      event.target;

    setIncidentForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  function openCreateForm() {
    setEditingIncident(null);

    setIncidentForm({
      ...emptyIncident,
    });

    setError("");
    setShowForm(true);
  }

  function openEditForm(incident) {
    if (!isAdmin) {
      setError(
        "Demo users cannot edit incidents."
      );

      return;
    }

    setEditingIncident(incident);

    setIncidentForm({
      title: incident.title,
      description: incident.description,
      severity: incident.severity,
      status: incident.status,
      owner: incident.owner,
    });

    setError("");
    setShowForm(true);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  function closeForm() {
    setShowForm(false);
    setEditingIncident(null);

    setIncidentForm({
      ...emptyIncident,
    });
  }

  async function handleSubmitIncident(
    event
  ) {
    event.preventDefault();

    const isEditing =
      editingIncident !== null;

    if (isEditing && !isAdmin) {
      setError(
        "Demo users cannot edit incidents."
      );

      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const accessToken =
        await getAccessToken();

      const url = isEditing
        ? `${
            import.meta.env.VITE_API_URL
          }/api/incidents/${encodeURIComponent(
            editingIncident.id
          )}`
        : `${
            import.meta.env.VITE_API_URL
          }/api/incidents`;

      const response = await fetch(
        url,
        {
          method: isEditing
            ? "PUT"
            : "POST",

          headers: {
            Authorization:
              `Bearer ${accessToken}`,

            "Content-Type":
              "application/json",
          },

          body: JSON.stringify(
            incidentForm
          ),
        }
      );

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error(
            "You do not have permission to perform this action."
          );
        }

        throw new Error(
          `Request failed: ${response.status}`
        );
      }

      closeForm();

      await loadIncidents();
    } catch (err) {
      console.error(err);

      setError(
        err.message ||
          (
            editingIncident
              ? "Unable to update incident."
              : "Unable to create incident."
          )
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDeleteIncident(
    incident
  ) {
    if (!isAdmin) {
      setError(
        "Demo users cannot delete incidents."
      );

      return;
    }

    const confirmed =
      window.confirm(
        `Delete "${incident.title}"?`
      );

    if (!confirmed) {
      return;
    }

    setError("");

    try {
      const accessToken =
        await getAccessToken();

      const response = await fetch(
        `${
          import.meta.env.VITE_API_URL
        }/api/incidents/${encodeURIComponent(
          incident.id
        )}`,
        {
          method: "DELETE",

          headers: {
            Authorization:
              `Bearer ${accessToken}`,
          },
        }
      );

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error(
            "You do not have permission to delete incidents."
          );
        }

        throw new Error(
          `Delete failed: ${response.status}`
        );
      }

      await loadIncidents();
    } catch (err) {
      console.error(err);

      setError(
        err.message ||
          "Unable to delete incident."
      );
    }
  }

  if (loading) {
    return (
      <div className="center-screen">
        <p>Loading CloudOps...</p>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="login-page">
        <div className="login-card">
          <div className="brand">
            CloudOps
          </div>

          <h1>
            Incident Management
          </h1>

          <p className="subtitle">
            Sign in to access the
            operations dashboard.
          </p>

          <form onSubmit={handleLogin}>
            <label htmlFor="username">
              Username
            </label>

            <input
              id="username"
              type="text"
              value={username}
              onChange={(event) =>
                setUsername(
                  event.target.value
                )
              }
              required
            />

            <label htmlFor="password">
              Password
            </label>

            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(
                  event.target.value
                )
              }
              required
            />

            {error && (
              <p className="error-message">
                {error}
              </p>
            )}

            <button
              className="primary-button"
              type="submit"
            >
              Sign In
            </button>
          </form>
        </div>
      </div>
    );
  }

  const criticalCount =
    incidents.filter(
      (incident) =>
        incident.severity === "CRITICAL"
    ).length;

  const openCount =
    incidents.filter(
      (incident) =>
        incident.status === "OPEN"
    ).length;

  return (
    <div className="dashboard">
      <header className="topbar">
        <div className="brand">
          CloudOps
        </div>

        <div className="header-right">
          <span className="username">
            {user.username}
          </span>

          <button
            className="logout-button"
            onClick={handleLogout}
          >
            Logout
          </button>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="dashboard-heading">
          <div>
            <h1>
              Incident Management Dashboard
            </h1>

            <p>
              Monitor and manage
              operational incidents.
            </p>
          </div>

          <button
            className="primary-button new-button"
            onClick={
              showForm
                ? closeForm
                : openCreateForm
            }
          >
            {showForm
              ? "Cancel"
              : "+ New Incident"}
          </button>
        </div>

        {!isAdmin && (
          <div className="error-banner">
            Demo access: you can view and
            create incidents. Editing and
            deleting are restricted.
          </div>
        )}

        {error && (
          <div className="error-banner">
            {error}
          </div>
        )}

        {showForm && (
          <section className="create-panel">
            <h2>
              {editingIncident
                ? "Edit Incident"
                : "Create Incident"}
            </h2>

            <form
              className="incident-form"
              onSubmit={
                handleSubmitIncident
              }
            >
              <div className="form-group">
                <label htmlFor="title">
                  Title
                </label>

                <input
                  id="title"
                  name="title"
                  value={
                    incidentForm.title
                  }
                  onChange={
                    handleFormChange
                  }
                  placeholder="Incident title"
                  required
                />
              </div>

              <div className="form-group full-width">
                <label htmlFor="description">
                  Description
                </label>

                <textarea
                  id="description"
                  name="description"
                  value={
                    incidentForm.description
                  }
                  onChange={
                    handleFormChange
                  }
                  placeholder="Describe the incident..."
                  rows="4"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="severity">
                  Severity
                </label>

                <select
                  id="severity"
                  name="severity"
                  value={
                    incidentForm.severity
                  }
                  onChange={
                    handleFormChange
                  }
                >
                  <option value="LOW">
                    Low
                  </option>

                  <option value="MEDIUM">
                    Medium
                  </option>

                  <option value="HIGH">
                    High
                  </option>

                  <option value="CRITICAL">
                    Critical
                  </option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="status">
                  Status
                </label>

                <select
                  id="status"
                  name="status"
                  value={
                    incidentForm.status
                  }
                  onChange={
                    handleFormChange
                  }
                >
                  <option value="OPEN">
                    Open
                  </option>

                  <option value="INVESTIGATING">
                    Investigating
                  </option>

                  <option value="RESOLVED">
                    Resolved
                  </option>

                  <option value="CLOSED">
                    Closed
                  </option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="owner">
                  Owner
                </label>

                <input
                  id="owner"
                  name="owner"
                  value={
                    incidentForm.owner
                  }
                  onChange={
                    handleFormChange
                  }
                  placeholder="Incident owner"
                  required
                />
              </div>

              <div className="form-actions">
                <button
                  className="secondary-button"
                  type="button"
                  onClick={closeForm}
                >
                  Cancel
                </button>

                <button
                  className="primary-button"
                  type="submit"
                  disabled={submitting}
                >
                  {submitting
                    ? "Saving..."
                    : editingIncident
                      ? "Save Changes"
                      : "Create Incident"}
                </button>
              </div>
            </form>
          </section>
        )}

        <section className="stats">
          <div className="stat-card">
            <span>
              Total Incidents
            </span>

            <strong>
              {incidents.length}
            </strong>
          </div>

          <div className="stat-card">
            <span>
              Critical
            </span>

            <strong>
              {criticalCount}
            </strong>
          </div>

          <div className="stat-card">
            <span>
              Open
            </span>

            <strong>
              {openCount}
            </strong>
          </div>
        </section>

        <section className="incident-section">
          <div className="section-heading">
            <h2>
              Incidents
            </h2>
          </div>

          {incidents.length === 0 ? (
            <p className="empty-message">
              No incidents found.
            </p>
          ) : (
            <div className="incident-list">
              {incidents.map(
                (incident) => (
                  <article
                    className="incident-card"
                    key={incident.id}
                  >
                    <div className="incident-info">
                      <h3>
                        {incident.title}
                      </h3>

                      <p>
                        {
                          incident.description
                        }
                      </p>

                      <small>
                        Owner:{" "}
                        {incident.owner}
                      </small>
                    </div>

                    <div className="incident-side">
                      <div className="incident-tags">
                        <span
                          className={`tag severity-${incident.severity.toLowerCase()}`}
                        >
                          {incident.severity}
                        </span>

                        <span className="tag">
                          {incident.status}
                        </span>
                      </div>

                      {isAdmin && (
                        <div className="incident-actions">
                          <button
                            className="edit-button"
                            onClick={() =>
                              openEditForm(
                                incident
                              )
                            }
                          >
                            Edit
                          </button>

                          <button
                            className="delete-button"
                            onClick={() =>
                              handleDeleteIncident(
                                incident
                              )
                            }
                          >
                            Delete
                          </button>
                        </div>
                      )}
                    </div>
                  </article>
                )
              )}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;